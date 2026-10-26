package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Divisao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisaoRepository extends JpaRepository<Divisao, Long> {
}
