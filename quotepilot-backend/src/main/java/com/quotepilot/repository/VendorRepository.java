package com.quotepilot.repository;

import com.quotepilot.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendorRepository
        extends JpaRepository<Vendor, String> {

    List<Vendor> findAllByVendorIdIn(List<String> vendorIds);

}
