package com.jorge.portfolio.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.common.exception.GlobalExceptionHandler;
import com.jorge.portfolio.common.exception.ResourceNotFoundException;
import com.jorge.portfolio.member.dto.MemberCreateRequest;
import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.member.enums.MemberAssignment;
import com.jorge.portfolio.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTest {

    private final MemberService memberService = mock(MemberService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MemberController(memberService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateMember() throws Exception {
        MemberCreateRequest request = new MemberCreateRequest("Ana Souza", MemberAssignment.FUNCIONARIO);
        MemberResponse response = memberResponse();

        when(memberService.create(any(MemberCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/external/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ana Souza"))
                .andExpect(jsonPath("$.assignment").value("FUNCIONARIO"));

        verify(memberService).create(any(MemberCreateRequest.class));
    }

    @Test
    void shouldFindMemberById() throws Exception {
        when(memberService.findById(1L)).thenReturn(memberResponse());

        mockMvc.perform(get("/api/external/members/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ana Souza"));

        verify(memberService).findById(1L);
    }

    @Test
    void shouldReturnBadRequestWhenBusinessExceptionIsThrown() throws Exception {
        when(memberService.create(any(MemberCreateRequest.class)))
                .thenThrow(new BusinessException("Já existe um membro cadastrado com este nome."));

        MemberCreateRequest request = new MemberCreateRequest("Ana Souza", MemberAssignment.FUNCIONARIO);

        mockMvc.perform(post("/api/external/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Já existe um membro cadastrado com este nome."))
                .andExpect(jsonPath("$.path").value("/api/external/members"));
    }

    @Test
    void shouldReturnNotFoundWhenResourceNotFoundExceptionIsThrown() throws Exception {
        when(memberService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Membro não encontrado."));

        mockMvc.perform(get("/api/external/members/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Membro não encontrado."))
                .andExpect(jsonPath("$.path").value("/api/external/members/99"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        MemberCreateRequest request = new MemberCreateRequest("", null);

        mockMvc.perform(post("/api/external/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.assignment").exists());
    }

    private MemberResponse memberResponse() {
        return new MemberResponse(
                1L,
                "Ana Souza",
                MemberAssignment.FUNCIONARIO,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }
}
