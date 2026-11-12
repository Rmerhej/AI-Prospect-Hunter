package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class ProspectSearchServiceTest {

    @Mock
    private ProspectRepository prospectRepository;

    @InjectMocks
    private ProspectSearchService prospectSearchService;

    @Test
    void shouldSearchWithFiltersAndPagination() {

        when(prospectRepository.findAll(
                ArgumentMatchers.<Specification<Prospect>>any(),
                ArgumentMatchers.<Pageable>any()
        )).thenReturn(Page.empty());

        Page<Prospect> result = prospectSearchService.search(
                "Secours",
                "Chatou",
                "Prioritaire",
                0,
                10
        );

        assertNotNull(result);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(prospectRepository).findAll(
                ArgumentMatchers.<Specification<Prospect>>any(),
                captor.capture()
        );

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertTrue(pageable.getSort().isSorted());
    }
}