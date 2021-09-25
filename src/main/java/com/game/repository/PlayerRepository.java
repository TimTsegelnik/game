package com.game.repository;

import com.game.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link Player} class.
 */
public interface PlayerRepository extends JpaRepository<Player,Long> {
}
