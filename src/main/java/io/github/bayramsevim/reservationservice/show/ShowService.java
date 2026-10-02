package io.github.bayramsevim.reservationservice.show;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShowService {
    private final ShowRepository showRepository;

    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @Transactional
    public ShowResponse create(CreateShowRequest request) {
        Show show = showRepository.save(new Show(request.title(), request.location(),
                request.startsAt(), request.saleStartsAt(), request.saleEndsAt()));
        return ShowResponse.from(show);
    }
}
