package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import br.com.itaipu.grfe.repository.projecao.ContagemPorNome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    List<Chamado> findByStatus(StatusChamado status);

    @Query("""
            select count(c) from Chamado c
            where c.dataHoraAcionamento >= :inicio and c.dataHoraAcionamento <= :fim
            """)
    long contarNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            select new br.com.itaipu.grfe.repository.projecao.ContagemPorNome(e.nome, count(c))
            from Chamado c join c.especialidade e
            where c.dataHoraAcionamento >= :inicio and c.dataHoraAcionamento <= :fim
            group by e.nome
            order by count(c) desc, e.nome asc
            """)
    List<ContagemPorNome> contarPorEspecialidade(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim);

    @Query("""
            select new br.com.itaipu.grfe.repository.projecao.ContagemPorNome(p.nome, count(c))
            from Chamado c join c.plantonista p
            where c.dataHoraAcionamento >= :inicio and c.dataHoraAcionamento <= :fim
            group by p.nome
            order by count(c) desc, p.nome asc
            """)
    List<ContagemPorNome> contarPorPlantonista(@Param("inicio") LocalDateTime inicio,
                                               @Param("fim") LocalDateTime fim);

    @Query("""
            select new br.com.itaipu.grfe.repository.projecao.ContagemPorNome(d.nome, count(c))
            from Chamado c join c.especialidade e join e.divisao d
            where c.dataHoraAcionamento >= :inicio and c.dataHoraAcionamento <= :fim
            group by d.nome
            order by count(c) desc, d.nome asc
            """)
    List<ContagemPorNome> contarPorDivisao(@Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim);

    @Query("""
            select new br.com.itaipu.grfe.repository.projecao.ContagemPorNome(d.nome, count(c))
            from Chamado c join c.especialidade e join e.departamento d
            where c.dataHoraAcionamento >= :inicio and c.dataHoraAcionamento <= :fim
            group by d.nome
            order by count(c) desc, d.nome asc
            """)
    List<ContagemPorNome> contarPorDepartamento(@Param("inicio") LocalDateTime inicio,
                                                @Param("fim") LocalDateTime fim);

}