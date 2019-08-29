package gg.paymentservice.api;

import gg.paymentservice.statistics.Statistics;
import gg.paymentservice.store.Accounts;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    @GetMapping(path = "/{second}")
    public Statistics get(@PathVariable("second") Integer second) {
        return Accounts.getStatistics(second);
    }

}
