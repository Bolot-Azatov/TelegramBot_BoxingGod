package org.boxinggod.boxing_god_bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    private Long chatId;

    private String username;
    private String firstName;

    private Long balance;

    private Integer totalFights;

    private LocalDateTime registeredAt;

    @PrePersist
    public void init() {
        if (this.balance == null) this.balance = 1000L;
        if (this.totalFights == null) this.totalFights = 0;
        this.registeredAt = LocalDateTime.now();
    }
}