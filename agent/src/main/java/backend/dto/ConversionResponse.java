package backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversionResponse {
    private List<String> controller = new ArrayList<>();
    private List<String> controllerEgov = new ArrayList<>();
    private Report controllerReport;

    private List<String> service = new ArrayList<>();
    private List<String> serviceEgov = new ArrayList<>();
    private Report serviceReport;

    private List<String> serviceimpl = new ArrayList<>();
    private List<String> serviceimplEgov = new ArrayList<>();
    private Report serviceimplReport;

    private List<String> vo = new ArrayList<>();
    private List<String> voEgov = new ArrayList<>();
    private Report voReport;
}
