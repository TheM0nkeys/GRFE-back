package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    List<Chamado> findByStatus(StatusChamado status);

}