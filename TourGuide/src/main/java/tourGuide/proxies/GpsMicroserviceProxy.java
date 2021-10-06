package tourGuide.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.model.beans.AttractionBean;
import tourGuide.model.beans.VisitedLocationBean;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "GpsMicroservice", url = "${feign.url.gps}")
public interface GpsMicroserviceProxy {

    @GetMapping(value="/AttractionsList")
    List<AttractionBean> getAttractionsList();

    @GetMapping(value="/UserLocation")
    VisitedLocationBean getUserLocation (@RequestParam("userId") UUID userId);
}
