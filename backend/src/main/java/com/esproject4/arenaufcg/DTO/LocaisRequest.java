package com.esproject4.arenaufcg.DTO;

import java.util.List;
import java.util.UUID;

public record LocaisRequest(
    List<UUID> localIds
) {}
