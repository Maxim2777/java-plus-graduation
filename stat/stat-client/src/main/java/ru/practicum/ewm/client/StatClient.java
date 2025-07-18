package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
public class StatClient {

    private final DiscoveryClient discoveryClient;
    private final String serviceId = "stats-server"; // имя из spring.application.name

    private RestClient restClient() {
        ServiceInstance instance = discoveryClient.getInstances(serviceId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Stats server not available"));

        URI baseUri = URI.create("http://" + instance.getHost() + ":" + instance.getPort());
        return RestClient.builder().baseUrl(baseUri.toString()).build();
    }

    public void sendHit(EndpointHitDto hitDto) {
        restClient().post()
                .uri("/hit")
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("unique", unique);

        if (start != null && !start.isBlank()) {
            uriBuilder.queryParam("start", start);
        }
        if (end != null && !end.isBlank()) {
            uriBuilder.queryParam("end", end);
        }
        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris.toArray());
        }

        String uri = uriBuilder.build().toUriString();

        List<ViewStatsDto> response = restClient().get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return response != null ? response : List.of();
    }
}