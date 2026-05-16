package com.weverse.service;

import com.weverse.dto.MemberStatusResponse;
import com.weverse.dto.MembershipEvent;
import com.weverse.dto.SubscribeRequest;
import com.weverse.dto.SubscribeResponse;
import com.weverse.entity.Member;
import com.weverse.entity.MembershipGrade;
import com.weverse.exception.MemberNotFoundException;
import com.weverse.producer.MembershipProducer;
import com.weverse.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    static final String MEMBERSHIP_CACHE_KEY_PREFIX = "membership:";
    private static final String CACHE_FIELD_GRADE = "grade";
    private static final String CACHE_FIELD_END_AT = "subscribeEndAt";
    private static final int SUBSCRIBE_PERIOD_YEARS = 1;

    private final MemberRepository memberRepository;
    private final MembershipProducer membershipProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public SubscribeResponse subscribe(SubscribeRequest request) {
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusYears(SUBSCRIBE_PERIOD_YEARS);

        Member member = memberRepository.findById(request.getMemberId())
                .orElseGet(() -> Member.builder()
                        .memberId(request.getMemberId())
                        .membershipGrade(request.getGrade())
                        .subscribeStartAt(startAt)
                        .subscribeEndAt(endAt)
                        .build());

        member.updateMembership(request.getGrade(), startAt, endAt);
        member.updateArtistId(request.getArtistId());
        memberRepository.save(member);

        cacheMembership(request.getMemberId(), request.getGrade(), endAt);

        membershipProducer.publish(MembershipEvent.builder()
                .memberId(request.getMemberId())
                .grade(request.getGrade().name())
                .subscribeStartAt(startAt)
                .subscribeEndAt(endAt)
                .build());

        log.info("멤버십 구독 완료 - memberId: {}, grade: {}, 만료일: {}",
                request.getMemberId(), request.getGrade(), endAt);

        return SubscribeResponse.builder()
                .memberId(request.getMemberId())
                .grade(request.getGrade())
                .subscribeEndAt(endAt)
                .build();
    }

    @Transactional(readOnly = true)
    public MemberStatusResponse getMemberStatus(String memberId) {
        String cacheKey = MEMBERSHIP_CACHE_KEY_PREFIX + memberId;
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(cacheKey);

        if (!cached.isEmpty()) {
            MembershipGrade grade = MembershipGrade.valueOf((String) cached.get(CACHE_FIELD_GRADE));
            LocalDateTime subscribeEndAt = LocalDateTime.parse((String) cached.get(CACHE_FIELD_END_AT));
            return MemberStatusResponse.builder()
                    .memberId(memberId)
                    .grade(grade)
                    .subscribeEndAt(subscribeEndAt)
                    .build();
        }

        log.warn("멤버십 캐시 미스 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (member.getSubscribeEndAt() != null) {
            cacheMembership(memberId, member.getMembershipGrade(), member.getSubscribeEndAt());
        }

        return MemberStatusResponse.from(member);
    }

    private void cacheMembership(String memberId, MembershipGrade grade, LocalDateTime endAt) {
        String cacheKey = MEMBERSHIP_CACHE_KEY_PREFIX + memberId;
        redisTemplate.opsForHash().putAll(cacheKey, Map.of(
                CACHE_FIELD_GRADE, grade.name(),
                CACHE_FIELD_END_AT, endAt.toString()
        ));
    }
}
