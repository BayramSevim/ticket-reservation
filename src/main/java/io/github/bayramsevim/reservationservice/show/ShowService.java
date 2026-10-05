package io.github.bayramsevim.reservationservice.show;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShowService {
    private final ShowRepository showRepository;

    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public ShowResponse create(CreateShowRequest request) {
        Show show = showRepository.save(new Show(request.title(), request.location(),
                request.startsAt(), request.saleStartsAt(), request.saleEndsAt()));
        return ShowResponse.from(show);
    }


    @Transactional(readOnly = true)
    @Cacheable("shows")
    public List<ShowResponse> getShows() {
        return showRepository.findAll().stream().map(ShowResponse::from).toList();
    }
}
