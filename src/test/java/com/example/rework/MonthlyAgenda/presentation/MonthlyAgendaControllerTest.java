package com.example.rework.monthlyagenda.presentation;

import com.example.rework.monthlyagenda.application.dto.MonthlyAgendaRequestDto;
import com.example.rework.monthlyagenda.domain.MonthlyAgenda;
import com.example.rework.monthlyagenda.fixture.MonthlyAgendaFixture;
import com.example.rework.auth.MemberRole;
import com.example.rework.member.domain.Member;
import com.example.rework.util.ControllerTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MonthlyAgendaControllerTest extends ControllerTestSupport {

    private static Long monthlyAgendaId;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .name("김민우1234")
                .password("test1234")
                .userId("kbsserver@naver.com")
                .role(MemberRole.MEMBER)
                .state(true)
                .build();
        MonthlyAgenda monthlyAgenda = MonthlyAgenda.builder()
                .todo("이번달아젠다")
                .state(false)
                .member(member)
                .build();
        memberRepository.saveAndFlush(member);
        MonthlyAgenda savedAgenda = monthlyAgendaRepository.saveAndFlush(monthlyAgenda);
        monthlyAgendaId = savedAgenda.getId();
    }

    @DisplayName("회원은 이번달 아젠다를 생성할 수 있다. (토큰 필수)")
    @Test
    @WithMockUser(username = "kbsserver@naver.com", authorities = {"MEMBER"})
    void createTodo() throws Exception {
        //given
        String url = "/api/v1/monthlyAgenda/";
        MonthlyAgendaRequestDto.CreateMonthlyAgendaRequestDto createMonthlyAgendaRequestDto = MonthlyAgendaFixture.createAgenda();

        //when
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMonthlyAgendaRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        //then
        mvcResult.getResponse().setContentType("application/json;charset=UTF-8");
        String result = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper1 = new ObjectMapper();

        JsonNode jsonNode = objectMapper1.readTree(result);
        JsonNode resultData = jsonNode.get("data");
        Long id = resultData.get("agendaId").asLong();
        String todo = resultData.get("todo").asText();
        boolean state = resultData.get("state").asBoolean();

        assertAll(
                () -> assertThat(id).isNotNull(),
                () -> assertThat(todo).isEqualTo("생성된 테스트 아젠다"),
                () -> assertThat(state).isFalse()
        );
    }

    @DisplayName("회원은 이번달 아젠다를 조회할 수 있다. (토큰 필수)")
    @Test
    @WithMockUser(username = "kbsserver@naver.com", authorities = {"MEMBER"})
    void readTodo() throws Exception {
        //given
        String url = "/api/v1/monthlyAgenda/";

        MonthlyAgendaRequestDto.ReadMonthlyAgendaRequestDto readMonthlyAgendaRequestDto = MonthlyAgendaFixture.readAgenda();

        //when
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readMonthlyAgendaRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        mvcResult.getResponse().setContentType("application/json;charset=UTF-8");
        String result = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode resultData = jsonNode.get("data");

        Long id = resultData.get("agendaId").asLong();
        String todo = resultData.get("todo").asText();
        boolean state = resultData.get("state").asBoolean();
        String createTime = resultData.get("createTime").asText();

        assertAll(
                () -> assertThat(id).isNotNull(),
                () -> assertThat(todo).isEqualTo("이번달아젠다"),
                () -> assertThat(state).isFalse(),
                () -> assertThat(createTime).isEqualTo("2024-05")
        );
    }

    @DisplayName("회원은 이번달 아젠다를 수정할 수 있다. (토큰 필수)")
    @Test
    @WithMockUser(username = "kbsserver@naver.com", authorities = {"MEMBER"})
    void updateTodo() throws Exception {
        //given
        String url = "/api/v1/monthlyAgenda/";

        MonthlyAgendaRequestDto.UpdateMonthlyAgendaRequestDto updateMonthlyAgendaRequestDto = MonthlyAgendaFixture.updateAgenda(monthlyAgendaId);

        //when
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMonthlyAgendaRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        mvcResult.getResponse().setContentType("application/json;charset=UTF-8");
        String result = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode resultData = jsonNode.get("data");
        Long id = resultData.get("agendaId").asLong();
        String todo = resultData.get("todo").asText();
        boolean state = resultData.get("state").asBoolean();

        assertAll(
                () -> assertThat(id).isNotNull(),
                () -> assertThat(todo).isEqualTo("수정된 테스트 아젠다"),
                () -> assertThat(state).isFalse()
        );
    }

    @DisplayName("회원은 이번달 아젠다를 삭제할 수 있다. (토큰 필수)")
    @Test
    @WithMockUser(username = "kbsserver@naver.com", authorities = {"MEMBER"})
    void deleteTodo() throws Exception {
        //given
        String url = "/api/v1/monthlyAgenda/?monthlyAgendaId=" + monthlyAgendaId;

        //when
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        //then
        mvcResult.getResponse().setContentType("application/json;charset=UTF-8");
        String result = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode resultData = jsonNode.get("data");
        String dataResult = resultData.asText();

        assertAll(
                () -> assertThat(dataResult).isEqualTo("true")
        );
    }
}
