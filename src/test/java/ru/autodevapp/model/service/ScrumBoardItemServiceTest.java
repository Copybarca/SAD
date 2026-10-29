package ru.autodevapp.model.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.ScrumBoardItem;
import ru.autodevapp.model.repository.ScrumBoardItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrumBoardItemServiceTest {

    @Mock
    ScrumBoardItemRepository repository;

    @Test
    void filtersItemsByModule() {
        ScrumBoardItem item = new ScrumBoardItem("API", "Добавить", "planner");
        when(repository.findAllByModuleIgnoreCaseOrderByCreatedAtDescIdDesc("planner"))
                .thenReturn(List.of(item));

        List<ScrumBoardItem> result = new ScrumBoardItemService(repository).findAll("planner");

        assertThat(result).containsExactly(item);
    }

    @Test
    void missingItemProducesNotFoundError() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ScrumBoardItemService(repository).findById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
