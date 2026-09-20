package io.github.jasonsimpart.compat.create;

/** Additional outputs remain owned by the drain until exported or the block is broken. */
public interface DrainOutputs {
    void createdelightcore$dropPendingOutputs();
}
