package com.capellax.whatsapp_backend.shared.service;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class State<T, V> {
    private StatusNotification status;
    private T value;
    private V error;
}
