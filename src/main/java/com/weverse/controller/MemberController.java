package com.weverse.controller;

import com.weverse.dto.MemberStatusResponse;
import com.weverse.dto.SubscribeRequest;
import com.weverse.dto.SubscribeResponse;
import com.weverse.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/subscribe")
    public SubscribeResponse subscribe(@RequestBody @Valid SubscribeRequest request) {
        return memberService.subscribe(request);
    }

    @GetMapping("/{memberId}/status")
    public MemberStatusResponse getMemberStatus(@PathVariable String memberId) {
        return memberService.getMemberStatus(memberId);
    }
}
