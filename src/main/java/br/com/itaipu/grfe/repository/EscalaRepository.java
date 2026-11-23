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

    @Query("""
            select count(e) > 0 from Escala e
            where e.especialidade.id = :especialidadeId
              and e.dataHoraInicio < :fim
              and e.dataHoraFim > :inicio
            """)
    boolean existeSobreposicao(@Param("especialidadeId") Long especialidadeId,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fim") LocalDateTime fim);

    @Query("""
            select count(e) > 0 from Escala e
            where e.especialidade.id = :especialidadeId
              and e.dataHoraInicio < :fim
              and e.dataHoraFim > :inicio
              and e.id <> :ignorarId
            """)
    boolean existeSobreposicaoIgnorando(@Param("especialidadeId") Long especialidadeId,
                                        @Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim,
                                        @Param("ignorarId") Long ignorarId);
}