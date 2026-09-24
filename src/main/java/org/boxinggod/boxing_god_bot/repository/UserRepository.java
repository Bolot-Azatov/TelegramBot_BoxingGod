package org.boxinggod.boxing_god_bot.repository;

import org.boxinggod.boxing_god_bot.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
}