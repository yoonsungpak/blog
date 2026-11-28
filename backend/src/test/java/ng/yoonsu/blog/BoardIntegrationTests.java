package ng.yoonsu.blog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import ng.yoonsu.blog.board.domain.Board;
import ng.yoonsu.blog.board.repository.BoardRepository;
import ng.yoonsu.blog.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoardIntegrationTests extends ApiIntegrationTestSupport {

    @Autowired
    private BoardRepository boardRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void clearBoards() {
        boardRepository.deleteAllInBatch();
    }

    @Test
    void adminCreatesPublicBoardByDefaultAndDuplicateUrlReturnsConflict() throws Exception {
        var admin = mockAdmin();

        mvc.perform(post("/api/boards/").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"개발 기록","url":"development"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("BOARD_CREATED"))
                .andExpect(jsonPath("$.data.board.name").value("개발 기록"))
                .andExpect(jsonPath("$.data.board.public").value(true))
                .andExpect(jsonPath("$.data.board.createdBy").value("admin"));

        mvc.perform(post("/api/boards/").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"다른 게시판","url":"development"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_BOARD_URL"));
    }

    @Test
    void adminCanCreatePrivateBoard() throws Exception {
        mvc.perform(post("/api/boards").with(mockAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"비공개 기록","url":"private-notes","isPublic":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.board.public").value(false))
                .andExpect(jsonPath("$.data.board.createdAt").exists());

        var createdAt = entityManager.createNativeQuery("select created_at from boards where url = :url")
                .setParameter("url", "private-notes")
                .getSingleResult();
        assertThat(createdAt).isNotNull();
    }

    @Test
    void boardCreationRejectsMissingUrlAndNonAdmin() throws Exception {
        mvc.perform(post("/api/boards/").with(mockAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"개발 기록"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        when(userRepository.findByIdAndDeletedAtIsNull("visitor"))
                .thenReturn(Optional.of(new User("visitor", "test-hash")));
        mvc.perform(post("/api/boards/").with(loggedInAs("visitor")).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"개발 기록","url":"forbidden-board"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertThat(boardRepository.existsByUrl("forbidden-board")).isFalse();
    }

    @Test
    void boardCreationRejectsUrlsOutsideLowercaseLettersAndHyphens() throws Exception {
        var admin = mockAdmin();
        for (String url : List.of("", "Dev-notes", "dev_notes", "dev1", "개발")) {
            mvc.perform(post("/api/boards/").with(admin).contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"개발 기록","url":"%s"}
                                    """.formatted(url)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("게시판 URL에는 영문 소문자와 하이픈(-)만 사용할 수 있습니다."));
        }
    }

    @Test
    void adminCanPartiallyUpdateBoardVisibility() throws Exception {
        var board = boardRepository.save(new Board("공개 기록", "visibility-board", "admin", true));

        mvc.perform(patch("/api/boards/{boardId}", board.getId()).with(mockAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isPublic\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BOARD_UPDATED"));

        var updated = boardRepository.findById(board.getId()).orElseThrow();
        assertThat(updated.getPublic()).isFalse();
        assertThat(updated.getName()).isEqualTo(board.getName());
        assertThat(updated.getUrl()).isEqualTo(board.getUrl());
    }

    @Test
    void adminCanUpdateBoardNameAndUrl() throws Exception {
        var board = boardRepository.save(new Board("이전 이름", "old-board-url", "admin", false));

        mvc.perform(patch("/api/boards/{boardId}", board.getId()).with(mockAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"새 이름","url":"new-board-url"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BOARD_UPDATED"));

        var updated = boardRepository.findById(board.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("새 이름");
        assertThat(updated.getUrl()).isEqualTo("new-board-url");
        assertThat(updated.getPublic()).isFalse();
    }

    @Test
    void boardUpdateRejectsInvalidAndMissingIds() throws Exception {
        var admin = mockAdmin();
        for (String boardId : List.of("not-a-number", "9223372036854775808", String.valueOf(Long.MAX_VALUE))) {
            mvc.perform(patch("/api/boards/{boardId}", boardId).with(admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"isPublic\":true}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("BOARD_NOT_FOUND"));
        }
    }

    @Test
    void boardUpdateRejectsEmptyBodyFieldsAndInvalidValues() throws Exception {
        var board = boardRepository.save(new Board("검증 대상", "validate-board", "admin", true));
        var admin = mockAdmin();
        for (String body : List.of("{}", "{\"name\":\"   \"}", "{\"url\":\"Bad_URL\"}")) {
            mvc.perform(patch("/api/boards/{boardId}", board.getId()).with(admin)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
    }

    @Test
    void boardUpdateRejectsDuplicateUrlWithoutSavingOtherChanges() throws Exception {
        var board = boardRepository.save(new Board("변경 전", "update-source", "admin", true));
        boardRepository.save(new Board("다른 게시판", "update-conflict", "admin", true));

        mvc.perform(patch("/api/boards/{boardId}", board.getId()).with(mockAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"변경 후","url":"update-conflict"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_BOARD_URL"));

        var unchanged = boardRepository.findById(board.getId()).orElseThrow();
        assertThat(unchanged.getName()).isEqualTo(board.getName());
        assertThat(unchanged.getUrl()).isEqualTo(board.getUrl());
    }

    @Test
    void boardUpdateRequiresAdmin() throws Exception {
        var board = boardRepository.save(new Board("관리자 게시판", "admin-edit-board", "admin", true));
        when(userRepository.findByIdAndDeletedAtIsNull("visitor"))
                .thenReturn(Optional.of(new User("visitor", "test-hash")));

        mvc.perform(patch("/api/boards/{boardId}", board.getId()).with(loggedInAs("visitor"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isPublic\":false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        assertThat(boardRepository.findById(board.getId()).orElseThrow().getPublic()).isTrue();
    }

    @Test
    void adminCanDeletePersistedBoard() throws Exception {
        var board = boardRepository.save(new Board("삭제할 게시판", "delete-board", "admin", true));

        mvc.perform(delete("/api/boards/{boardId}", board.getId()).with(mockAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BOARD_DELETED"));

        assertThat(boardRepository.existsById(board.getId())).isFalse();
    }
}
