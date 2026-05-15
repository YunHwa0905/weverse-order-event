package com.weverse.service;

import com.weverse.exception.MemberNotFoundException;
import com.weverse.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void processMembership(Map<String, Object> payload) {
        String memberId = (String) payload.get("memberId");
        String grade = (String) payload.get("grade");

        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (member.getSubscribeEndAt() == null) {
            return;
        }

        String cacheKey = MemberService.MEMBERSHIP_CACHE_KEY_PREFIX + memberId;
        redisTemplate.opsForHash().putAll(cacheKey, Map.of(
                "grade", member.getMembershipGrade().name(),
                "subscribeEndAt", member.getSubscribeEndAt().toString()
        ));

        log.info("멤버십 캐시 갱신 완료 - memberId: {}, grade: {}", memberId, grade);
    }
}
