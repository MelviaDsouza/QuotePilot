
package com.quotepilot.repository;

import com.quotepilot.entity.Rfx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RfxRepository extends JpaRepository<Rfx, String> {
}
