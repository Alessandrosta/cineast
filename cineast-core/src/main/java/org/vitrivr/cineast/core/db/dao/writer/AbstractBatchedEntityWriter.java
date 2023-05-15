package org.vitrivr.cineast.core.db.dao.writer;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public abstract class AbstractBatchedEntityWriter<T> implements Closeable {

  /**
   * The {@link Queue} used to store {@link PersistentTuple}s until they are flushed to disk.
   */
  private final ArrayBlockingQueue<PersistentTuple> buffer;
  /**
   * Flag indicating whether inserts should be batched in memory and submitted all at once.
   */
  private final boolean batch;
  /**
   * {@link PersistencyWriter} instance used to persist changes to the underlying persistence layer.
   */
  protected PersistencyWriter<?> writer;

  private static final Logger LOGGER = LogManager.getLogger();

  protected AbstractBatchedEntityWriter(PersistencyWriter<?> writer) {
    this.batch = writer.supportedBatchSize() > 1;
    if (this.batch) {
      this.buffer = new ArrayBlockingQueue<>(writer.supportedBatchSize());
    } else {
      this.buffer = null; //not used
    }
    this.writer = writer;
  }

  protected abstract void init();

  protected abstract PersistentTuple generateTuple(T entity);

  /**
   * Persists the provided entity by first converting it to a {@link PersistentTuple} and subsequently writing that tuple to the local buffer. If the buffer is full, i.e. the batch size was reached, then buffer is flushed first.
   *
   * @param entity The entity that should be persisted.
   */
  public void write(T entity) {
    final PersistentTuple tuple = this.generateTuple(entity);
    if (tuple == null) {
      return; // One of the entity's value provider was a NothingProvider, hence nothing is written.
    }
    if (this.batch) {
      if (this.buffer.remainingCapacity() == 0) {
        this.flush();
      }
      this.buffer.offer(tuple);
    } else {
      this.writer.persist(tuple);
    }
  }

  public void write(List<T> entity) {
    entity.forEach(this::write);
  }

  /**
   * Drains the content of the buffer and writes it to the underlying persistence layer using the local {@link PersistencyWriter} instance.
   */
  public final void flush() {
    if (!this.batch) {
      return;
    }
    final List<PersistentTuple> batch = new ArrayList<>(buffer.size());
    this.buffer.drainTo(batch);
    this.writer.persist(batch);
  }

  /**
   * Flushes the buffer and closes the local {@link PersistencyWriter}.
   */
  @Override
  public final void close() {
    if (this.writer == null) {
      LOGGER.trace("Underlying writer is null, not doing anything upon close");
      return;
    }
    if (this.batch && this.buffer.size() > 0) {
      LOGGER.debug("Flushing writer upon close");
      this.flush();
    }

    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
  }

  public boolean idExists(String id) {
    return this.writer.idExists(id);
  }

}
