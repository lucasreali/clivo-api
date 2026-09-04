package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.scheduling.ScheduleBlockService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule-blocks")
class ScheduleBlockController {

    private final ScheduleBlockService blocks;

    ScheduleBlockController(ScheduleBlockService blocks) {
        this.blocks = blocks;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ScheduleBlockView register(@Valid @RequestBody ScheduleBlockRequest request) {
        return ScheduleBlockView.of(blocks.register(request.toDetails()));
    }

    @GetMapping
    List<ScheduleBlockView> findWithin(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return blocks.findWithin(TimeWindow.of(from, to)).stream().map(ScheduleBlockView::of).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void release(@PathVariable Long id) {
        blocks.release(id);
    }
}
