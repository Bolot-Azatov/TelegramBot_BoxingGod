package org.boxinggod.boxing_god_bot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_boxers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomBoxer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId; // Привязка к игроку

    private String name;        // Имя бойца (например, "Иван 'Молот' Громов")
    private String stance;      // Правша / Левша
    private String style;       // Инфайтер, Аутфайтер, Слаггер, Контрпанчер

    // Характеристики (от 1 до 100)
    private int power;          // Сила удара
    private int speed;          // Скорость
    private int stamina;        // Выносливость
    private int chin;           // Крепость челюсти
    private int ringIq;         // Боксерский IQ

    private int wins;           // Побед
    private int losses;         // Поражений
}