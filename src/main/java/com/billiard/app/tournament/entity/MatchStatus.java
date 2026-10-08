package com.billiard.app.tournament.entity;

public enum MatchStatus {
    /** Chưa đủ 2 người chơi (đang chờ trận trước đó hoàn thành). */
    PENDING,
    /** Đã đủ 2 người chơi, sẵn sàng ghi nhận kết quả. */
    READY,
    COMPLETED
}
