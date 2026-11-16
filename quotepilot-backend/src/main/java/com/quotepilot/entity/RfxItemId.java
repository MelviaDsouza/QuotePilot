package com.quotepilot.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class RfxItemId implements Serializable {

    private String rfxId;
    private String itemId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfxItemId that)) return false;

        return Objects.equals(rfxId, that.rfxId)
                && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rfxId, itemId);
    }
}