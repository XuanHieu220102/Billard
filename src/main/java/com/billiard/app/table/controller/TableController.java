package com.billiard.app.table.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.table.dto.CreateTableRequest;
import com.billiard.app.table.dto.TableResponse;
import com.billiard.app.table.dto.UpdateTableRequest;
import com.billiard.app.table.service.TableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public ApiResponse<List<TableResponse>> listTables() {
        return ApiResponse.success(tableService.listTables());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TableResponse> createTable(@Valid @RequestBody CreateTableRequest request) {
        return ApiResponse.success(tableService.createTable(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TableResponse> updateTable(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateTableRequest request) {
        return ApiResponse.success(tableService.updateTable(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<TableResponse> deactivateTable(@PathVariable UUID id) {
        return ApiResponse.success(tableService.deactivateTable(id));
    }
}
