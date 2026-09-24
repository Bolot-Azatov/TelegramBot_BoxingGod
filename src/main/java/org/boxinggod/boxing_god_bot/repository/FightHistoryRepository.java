package org.boxinggod.boxing_god_bot.repository;

import org.boxinggod.boxing_god_bot.entity.FightHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FightHistoryRepository extends JpaRepository<FightHistory, Long> {
    List<FightHistory> findTop5ByChatIdOrderByFoughtAtDesc(Long chatId);
}