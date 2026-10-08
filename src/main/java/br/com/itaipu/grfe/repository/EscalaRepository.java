package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long> {
}
