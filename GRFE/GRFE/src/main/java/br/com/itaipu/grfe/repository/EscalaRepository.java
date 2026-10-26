package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long> {

    @Query("""
            select count(e) > 0 from Escala e
            where e.especialidade.id = :especialidadeId
              and e.funcionario.id = :funcionarioId
              and e.dataHoraInicio <= :momento
              and e.dataHoraFim >= :momento
            """)
    boolean plantonistaEmEscala(@Param("especialidadeId") Long especialidadeId,
                                @Param("funcionarioId") Long funcionarioId,
                                @Param("momento") LocalDateTime momento);
}