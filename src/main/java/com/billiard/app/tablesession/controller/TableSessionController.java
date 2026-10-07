package com.billiard.app.tablesession.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.tablesession.dto.CloseTableSessionResponse;
import com.billiard.app.tablesession.dto.OpenTableSessionRequest;
import com.billiard.app.tablesession.dto.TableSessionResponse;
import com.billiard.app.tablesession.service.TableSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/table-sessions")
public class TableSessionController {

    private final TableSessionService tableSessionService;

    public TableSessionController(TableSessionService tableSessionService) {
        this.tableSessionService = tableSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TableSessionResponse> openSession(@Valid @RequestBody OpenTableSessionRequest request) {
        return ApiResponse.success(tableSessionService.openSession(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TableSessionResponse> getSession(@PathVariable UUID id) {
        return ApiResponse.success(tableSessionService.getSession(id));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<CloseTableSessionResponse> closeSession(@PathVariable UUID id) {
        return ApiResponse.success(tableSessionService.closeSession(id));
    }
}
