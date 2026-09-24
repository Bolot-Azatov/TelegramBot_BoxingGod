package org.boxinggod.boxing_god_bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fight_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FightHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private String boxer1;
    private String boxer2;

    @Column(columnDefinition = "TEXT")
    private String simulationResult;

    private LocalDateTime foughtAt;

    @PrePersist
    public void init() {
        this.foughtAt = LocalDateTime.now();
    }
}