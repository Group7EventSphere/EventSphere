package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AdRepositoryTest {

    @Autowired
    private AdRepository repo;

    @Test
    void testSaveAd() {
        Ad a = Ad.builder()
                .title("T1")
                .description("D1")
                .imageUrl("i1.jpg")
                .active(true)
                .build();

        Ad saved = repo.save(a);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("T1");
    }

    @Test
    void testFindById() {
        Ad saved = repo.save(
                Ad.builder().title("X").description("Y").imageUrl("z.png").active(true).build()
        );

        Optional<Ad> found = repo.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Y");
    }

    @Test
    void testFindById_NotFound() {
        Optional<Ad> none = repo.findById(999L);
        assertThat(none).isNotPresent();
    }

    @Test
    void testFindAllAds() {
        repo.save(Ad.builder().title("A").description("a").imageUrl("a.jpg").active(true).build());
        repo.save(Ad.builder().title("B").description("b").imageUrl("b.jpg").active(true).build());

        List<Ad> all = repo.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void testCountAds() {
        long before = repo.count();
        repo.save(Ad.builder().title("C").description("c").imageUrl("c.jpg").active(true).build());
        assertThat(repo.count()).isEqualTo(before + 1);
    }

    @Test
    void testDeleteAd() {
        Ad saved = repo.save(
                Ad.builder().title("Del").description("d").imageUrl("d.jpg").active(true).build()
        );
        repo.deleteById(saved.getId());
        assertThat(repo.findById(saved.getId())).isNotPresent();
    }

    @Test
    void testUpdateAd() {
        Ad saved = repo.save(
                Ad.builder().title("Old").description("d").imageUrl("i.jpg").active(true).build()
        );
        saved.setTitle("New");
        Ad updated = repo.save(saved);

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(repo.findById(saved.getId()).get().getTitle()).isEqualTo("New");
    }

    @Test
    void testFindByActiveTrue() {
        repo.save(Ad.builder().title("On").description("1").imageUrl("on.jpg").active(true).build());
        repo.save(Ad.builder().title("Off").description("2").imageUrl("off.jpg").active(false).build());

        List<Ad> activeList = repo.findByActiveTrue();

        assertThat(activeList)
                .isNotEmpty()
                // verify every element really is active
                .allSatisfy(ad -> assertThat(ad.isActive()).isTrue());
    }
}
