package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.HistoricoAcionamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoAcionamentoRepository extends JpaRepository<HistoricoAcionamento, Long> {
}
