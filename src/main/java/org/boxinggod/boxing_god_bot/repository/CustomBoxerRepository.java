package org.boxinggod.boxing_god_bot.repository;

import org.boxinggod.boxing_god_bot.entity.CustomBoxer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomBoxerRepository extends JpaRepository<CustomBoxer, Long> {
    Optional<CustomBoxer> findByChatId(Long chatId);
}