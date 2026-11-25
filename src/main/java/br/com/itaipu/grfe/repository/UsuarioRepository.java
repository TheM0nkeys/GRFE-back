package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "funcionario")
    Optional<Usuario> findByLogin(String login);

}