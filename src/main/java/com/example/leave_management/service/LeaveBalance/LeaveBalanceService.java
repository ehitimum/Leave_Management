package com.example.leave_management.service.LeaveBalance;

import com.example.leave_management.dto.LeaveBalance.CustomBalanceSetter;
import com.example.leave_management.dto.LeaveBalance.CustomLeaveBalanceSetResponse;
import com.example.leave_management.domain.model.User.Balance.LeaveBalance;
import com.example.leave_management.domain.model.User.User;
import com.example.leave_management.domain.repository.LeaveBalanceRepository;
import com.example.leave_management.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;

    public LeaveBalanceService(LeaveBalanceRepository leaveBalanceRepository, UserRepository userRepository) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userRepository = userRepository;
    }

    public CustomLeaveBalanceSetResponse setCustomBalanceForNewUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var balance = LeaveBalance.builder()
                .sickLeaveBalance(5)
                .earnedLeaveBalance(0)
                .negativeBalance(0)
                .user(user)
                .build();
        leaveBalanceRepository.save(balance);
        return CustomLeaveBalanceSetResponse.builder().response("New User Leave Balance has set.").build();
    }

    @Transactional
    public CustomLeaveBalanceSetResponse setCustomBalance(CustomBalanceSetter request, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var balance = LeaveBalance.builder()
                .sickLeaveBalance(request.getSickLeaveBalance())
                .earnedLeaveBalance(request.getEarnedLeaveBalance())
                .negativeBalance(request.getNegativeBalance())
                .user(user)
                .build();
        leaveBalanceRepository.save(balance);
        return CustomLeaveBalanceSetResponse.builder().response("A new balance is updated.").build();
    }

    public void balanceDeduction(Long userId){
        LeaveBalance userBalance = leaveBalanceRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for this user"));

        int sickBal = userBalance.getSickLeaveBalance();
        int earnedBal = userBalance.getEarnedLeaveBalance();
        int bunked = userBalance.getNegativeBalance();

        if(sickBal != 0){
            userBalance.setSickLeaveBalance(sickBal-1);
        } else if (earnedBal != 0) {
            userBalance.setEarnedLeaveBalance(earnedBal-1);
        }else{
            userBalance.setNegativeBalance(bunked-1);
        }
        leaveBalanceRepository.save(userBalance);
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void monthlyLeaveBalanceUpdate() {
        System.out.println("Scheduled method executed at: " + LocalDateTime.now());
        List<LeaveBalance> allLeaveBalances = leaveBalanceRepository.findAll();

        for (LeaveBalance userBalance : allLeaveBalances) {
            int currentMonth = LocalDate.now().getMonthValue();
            if (currentMonth <= 10) {
                userBalance.setEarnedLeaveBalance(userBalance.getEarnedLeaveBalance() + 1);
            } else if (currentMonth == 11) {
                userBalance.setSickLeaveBalance(5);
                userBalance.setEarnedLeaveBalance(0);
            }
            if (userBalance.getNegativeBalance() < 0) {
                int earnedToAdjust = Math.min(-userBalance.getNegativeBalance(), userBalance.getEarnedLeaveBalance());
                userBalance.setEarnedLeaveBalance(userBalance.getEarnedLeaveBalance() - earnedToAdjust);
                userBalance.setNegativeBalance(userBalance.getNegativeBalance() + earnedToAdjust);
            }
        }

        leaveBalanceRepository.saveAll(allLeaveBalances);
    }
}
