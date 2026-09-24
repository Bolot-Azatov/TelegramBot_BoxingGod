package org.boxinggod.boxing_god_bot.service;

import lombok.RequiredArgsConstructor;
import org.boxinggod.boxing_god_bot.entity.AppUser;
import org.boxinggod.boxing_god_bot.entity.CustomBoxer;
import org.boxinggod.boxing_god_bot.entity.FightHistory;
import org.boxinggod.boxing_god_bot.repository.CustomBoxerRepository;
import org.boxinggod.boxing_god_bot.repository.FightHistoryRepository;
import org.boxinggod.boxing_god_bot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FightHistoryRepository historyRepository;
    private final CustomBoxerRepository boxerRepository;

    @Transactional
    public AppUser getOrCreateUser(Long chatId, String username, String firstName) {
        return userRepository.findById(chatId).orElseGet(() -> {
            AppUser newUser = AppUser.builder()
                    .chatId(chatId)
                    .username(username)
                    .firstName(firstName)
                    .balance(1000L)
                    .totalFights(0)
                    .build();
            return userRepository.save(newUser);
        });
    }

    @Transactional
    public Optional<CustomBoxer> getCustomBoxer(Long chatId) {
        return boxerRepository.findByChatId(chatId);
    }

    @Transactional
    public CustomBoxer createDefaultBoxer(Long chatId, String name) {
        CustomBoxer boxer = CustomBoxer.builder()
                .chatId(chatId)
                .name(name)
                .stance("Правша")
                .style("Сбалансированный боксер-панчер")
                .power(80)
                .speed(80)
                .stamina(80)
                .chin(80)
                .ringIq(80)
                .wins(0)
                .losses(0)
                .build();
        return boxerRepository.save(boxer);
    }

    @Transactional
    public void recordCustomFightResult(Long chatId, boolean won) {
        boxerRepository.findByChatId(chatId).ifPresent(boxer -> {
            if (won) {
                boxer.setWins(boxer.getWins() + 1);
            } else {
                boxer.setLosses(boxer.getLosses() + 1);
            }
            boxerRepository.save(boxer);
        });
    }

    @Transactional
    public boolean placeBet(Long chatId, long betAmount) {
        AppUser user = userRepository.findById(chatId).orElse(null);
        if (user == null || user.getBalance() < betAmount) {
            return false;
        }
        user.setBalance(user.getBalance() - betAmount);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public long awardWin(Long chatId, long winAmount) {
        AppUser user = userRepository.findById(chatId).orElse(null);
        if (user != null) {
            user.setBalance(user.getBalance() + winAmount);
            userRepository.save(user);
            return user.getBalance();
        }
        return 0;
    }

    @Transactional
    public long getBalance(Long chatId) {
        return userRepository.findById(chatId).map(AppUser::getBalance).orElse(0L);
    }

    @Transactional
    public void recordFight(Long chatId, String boxer1, String boxer2, String result) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setTotalFights(user.getTotalFights() + 1);
            userRepository.save(user);
        });

        FightHistory history = FightHistory.builder()
                .chatId(chatId)
                .boxer1(boxer1)
                .boxer2(boxer2)
                .simulationResult(result)
                .build();
        historyRepository.save(history);
    }
}