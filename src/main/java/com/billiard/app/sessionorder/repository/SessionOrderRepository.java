package com.billiard.app.sessionorder.repository;

import com.billiard.app.sessionorder.entity.SessionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionOrderRepository extends JpaRepository<SessionOrder, UUID> {

    List<SessionOrder> findAllByTableSessionIdAndShopIdOrderByCreatedAtAsc(UUID tableSessionId, UUID shopId);

    Optional<SessionOrder> findByIdAndTableSessionIdAndShopId(UUID id, UUID tableSessionId, UUID shopId);

    @Query("select coalesce(sum(o.lineTotal), 0) from SessionOrder o where o.tableSessionId = :tableSessionId and o.shopId = :shopId")
    BigDecimal sumLineTotalByTableSessionIdAndShopId(UUID tableSessionId, UUID shopId);

    List<SessionOrder> findAllByShopIdAndTableSessionIdIn(UUID shopId, List<UUID> tableSessionIds);
}
