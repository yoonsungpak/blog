package ng.yoonsu.blog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import ng.yoonsu.blog.board.domain.Category;
import ng.yoonsu.blog.board.repository.CategoryRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoardIntegrationTests extends ApiIntegrationTestSupport {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void clearBoards() {
        boardRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
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
                .andExpect(jsonPath("$.data.board.displayOrder").doesNotExist())
                .andExpect(jsonPath("$.data.board.createdBy").value("admin"));

        mvc.perform(post("/api/boards/").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"다른 게시판","url":"development"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_BOARD_URL"));
    }

    @Test
    void adminCanManageCategoriesAndMoveBoards() throws Exception {
        var admin = mockAdmin();
        mvc.perform(post("/api/categories").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"개발\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.displayOrder").value(0));
        var category = categoryRepository.findAll().get(0);

        mvc.perform(put("/api/categories/{id}", category.getId()).with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"개발 기록\"}"))
                .andExpect(status().isOk());
        assertThat(categoryRepository.findById(category.getId()).orElseThrow().getName()).isEqualTo("개발 기록");

        mvc.perform(post("/api/boards").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"게시판","url":"category-board","categoryId":%d}
                                """.formatted(category.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.board.categoryId").value(category.getId()));
        var board = boardRepository.findAll().get(0);

        mvc.perform(patch("/api/boards/{id}", board.getId()).with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"category\":{\"id\":null}}"))
                .andExpect(status().isOk());
        assertThat(boardRepository.findById(board.getId()).orElseThrow().getCategoryId()).isNull();

        mvc.perform(patch("/api/boards/{id}", board.getId()).with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":{\"id\":%d}}".formatted(category.getId())))
                .andExpect(status().isOk());
        assertThat(boardRepository.findById(board.getId()).orElseThrow().getCategoryId()).isEqualTo(category.getId());

        mvc.perform(delete("/api/categories/{id}", category.getId()).with(admin))
                .andExpect(status().isOk());
        assertThat(categoryRepository.existsById(category.getId())).isFalse();
        assertThat(boardRepository.findById(board.getId()).orElseThrow().getCategoryId()).isNull();
    }

    @Test
    void adminCanReorderCategoriesWithoutChangingBoardOrder() throws Exception {
        var first = categoryRepository.save(new Category("첫 카테고리"));
        var second = categoryRepository.save(new Category("둘째 카테고리"));
        var board = new Board("게시판", "board-in-category", "admin", true);
        board.moveToCategory(first);
        board.setDisplayOrder(3);
        boardRepository.save(board);

        var admin = mockAdmin();
        mvc.perform(patch("/api/categories/order").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[%d,%d]}".formatted(second.getId(), first.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BOARD_CATEGORY_ORDER_UPDATED"));
        mvc.perform(patch("/api/categories/order").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[%d,%d]}".formatted(first.getId(), first.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOARD_CATEGORY_ORDER_MISMATCH"));

        assertThat(categoryRepository.findById(second.getId()).orElseThrow().getDisplayOrder()).isEqualTo(1);
        assertThat(categoryRepository.findById(first.getId()).orElseThrow().getDisplayOrder()).isEqualTo(2);
        assertThat(boardRepository.findById(board.getId()).orElseThrow().getDisplayOrder()).isEqualTo(3);
    }

    @Test
    void boardReorderingCannotCrossCategoryBoundaries() throws Exception {
        var category = categoryRepository.save(new Category("개발"));
        var first = new Board("첫 게시판", "category-first", "admin", true);
        first.moveToCategory(category);
        boardRepository.save(first);
        var second = new Board("둘째 게시판", "category-second", "admin", false);
        second.moveToCategory(category);
        boardRepository.save(second);
        var ungrouped = boardRepository.save(new Board("미분류 게시판", "ungrouped-board", "admin", true));
        var admin = mockAdmin();

        mvc.perform(patch("/api/boards/order").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":%d,\"boardIds\":[%d,%d]}"
                                .formatted(category.getId(), second.getId(), first.getId())))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/boards/order").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":%d,\"boardIds\":[%d,%d]}"
                                .formatted(category.getId(), ungrouped.getId(), second.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOARD_ORDER_MISMATCH"));

        assertThat(boardRepository.findById(second.getId()).orElseThrow().getDisplayOrder()).isEqualTo(1);
        assertThat(boardRepository.findById(first.getId()).orElseThrow().getDisplayOrder()).isEqualTo(2);
        assertThat(boardRepository.findById(ungrouped.getId()).orElseThrow().getDisplayOrder()).isZero();
    }

    @Test
    void boardDisplayOrderCanOnlyBeChangedThroughReorderEndpoint() throws Exception {
        var admin = mockAdmin();
        mvc.perform(post("/api/boards").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"순서 지정","url":"ordered-board","displayOrder":3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.board.displayOrder").doesNotExist());
        var board = boardRepository.findAll().get(0);
        assertThat(board.getDisplayOrder()).isZero();

        mvc.perform(patch("/api/boards/order").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardIds\":[%d]}".formatted(board.getId())))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/boards/{boardId}", board.getId()).with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"새 이름","displayOrder":0}
                                """))
                .andExpect(status().isOk());

        var updated = boardRepository.findById(board.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("새 이름");
        assertThat(updated.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void adminCanSaveDisplayOrderForAllBoards() throws Exception {
        var first = boardRepository.save(new Board("첫 게시판", "order-first", "admin", true));
        var second = boardRepository.save(new Board("둘째 게시판", "order-second", "admin", false));

        mvc.perform(patch("/api/boards/order").with(mockAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardIds\":[%d,%d]}".formatted(second.getId(), first.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BOARD_ORDER_UPDATED"));

        assertThat(boardRepository.findById(second.getId()).orElseThrow().getDisplayOrder()).isEqualTo(1);
        assertThat(boardRepository.findById(first.getId()).orElseThrow().getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void boardReorderRejectsUnauthorizedAndOutdatedLists() throws Exception {
        var first = boardRepository.save(new Board("첫 게시판", "reorder-first", "admin", true));
        var second = boardRepository.save(new Board("둘째 게시판", "reorder-second", "admin", true));
        when(userRepository.findByIdAndDeletedAtIsNull("visitor"))
                .thenReturn(Optional.of(new User("visitor", "test-hash")));

        mvc.perform(patch("/api/boards/order").with(loggedInAs("visitor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardIds\":[%d,%d]}".formatted(second.getId(), first.getId())))
                .andExpect(status().isForbidden());

        var admin = mockAdmin();
        for (String ids : List.of("%d".formatted(first.getId()),
                "%d,%d".formatted(first.getId(), first.getId()),
                "%d,%d".formatted(first.getId(), Long.MAX_VALUE))) {
            mvc.perform(patch("/api/boards/order").with(admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"boardIds\":[" + ids + "]}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("BOARD_ORDER_MISMATCH"));
        }

        assertThat(boardRepository.findAll()).allMatch(board -> board.getDisplayOrder() == 0);
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
