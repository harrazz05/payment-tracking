package com.paytrack.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.paytrack.app.data.model.DailySummary;
import com.paytrack.app.data.model.TransferTransaction;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TransferDao_Impl implements TransferDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransferTransaction> __insertionAdapterOfTransferTransaction;

  private final EntityDeletionOrUpdateAdapter<TransferTransaction> __deletionAdapterOfTransferTransaction;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public TransferDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransferTransaction = new EntityInsertionAdapter<TransferTransaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `transfer_transactions` (`id`,`sender`,`amount`,`currency`,`timestamp`,`dateString`,`timeString`,`rawMessage`,`transactionId`,`notes`,`isSimulated`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransferTransaction entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSender());
        statement.bindDouble(3, entity.getAmount());
        statement.bindString(4, entity.getCurrency());
        statement.bindLong(5, entity.getTimestamp());
        statement.bindString(6, entity.getDateString());
        statement.bindString(7, entity.getTimeString());
        statement.bindString(8, entity.getRawMessage());
        if (entity.getTransactionId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getTransactionId());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotes());
        }
        final int _tmp = entity.isSimulated() ? 1 : 0;
        statement.bindLong(11, _tmp);
      }
    };
    this.__deletionAdapterOfTransferTransaction = new EntityDeletionOrUpdateAdapter<TransferTransaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `transfer_transactions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransferTransaction entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM transfer_transactions";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TransferTransaction transaction,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTransferTransaction.insertAndReturnId(transaction);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<TransferTransaction> transactions,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfTransferTransaction.insertAndReturnIdsList(transactions);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final TransferTransaction transaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTransferTransaction.handle(transaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransferTransaction>> getAllTransactions() {
    final String _sql = "SELECT * FROM transfer_transactions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transfer_transactions"}, new Callable<List<TransferTransaction>>() {
      @Override
      @NonNull
      public List<TransferTransaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final int _cursorIndexOfTimeString = CursorUtil.getColumnIndexOrThrow(_cursor, "timeString");
          final int _cursorIndexOfRawMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessage");
          final int _cursorIndexOfTransactionId = CursorUtil.getColumnIndexOrThrow(_cursor, "transactionId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsSimulated = CursorUtil.getColumnIndexOrThrow(_cursor, "isSimulated");
          final List<TransferTransaction> _result = new ArrayList<TransferTransaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransferTransaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final String _tmpTimeString;
            _tmpTimeString = _cursor.getString(_cursorIndexOfTimeString);
            final String _tmpRawMessage;
            _tmpRawMessage = _cursor.getString(_cursorIndexOfRawMessage);
            final String _tmpTransactionId;
            if (_cursor.isNull(_cursorIndexOfTransactionId)) {
              _tmpTransactionId = null;
            } else {
              _tmpTransactionId = _cursor.getString(_cursorIndexOfTransactionId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsSimulated;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSimulated);
            _tmpIsSimulated = _tmp != 0;
            _item = new TransferTransaction(_tmpId,_tmpSender,_tmpAmount,_tmpCurrency,_tmpTimestamp,_tmpDateString,_tmpTimeString,_tmpRawMessage,_tmpTransactionId,_tmpNotes,_tmpIsSimulated);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TransferTransaction>> getTransactionsByDate(final String date) {
    final String _sql = "SELECT * FROM transfer_transactions WHERE dateString = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transfer_transactions"}, new Callable<List<TransferTransaction>>() {
      @Override
      @NonNull
      public List<TransferTransaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final int _cursorIndexOfTimeString = CursorUtil.getColumnIndexOrThrow(_cursor, "timeString");
          final int _cursorIndexOfRawMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessage");
          final int _cursorIndexOfTransactionId = CursorUtil.getColumnIndexOrThrow(_cursor, "transactionId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsSimulated = CursorUtil.getColumnIndexOrThrow(_cursor, "isSimulated");
          final List<TransferTransaction> _result = new ArrayList<TransferTransaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransferTransaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final String _tmpTimeString;
            _tmpTimeString = _cursor.getString(_cursorIndexOfTimeString);
            final String _tmpRawMessage;
            _tmpRawMessage = _cursor.getString(_cursorIndexOfRawMessage);
            final String _tmpTransactionId;
            if (_cursor.isNull(_cursorIndexOfTransactionId)) {
              _tmpTransactionId = null;
            } else {
              _tmpTransactionId = _cursor.getString(_cursorIndexOfTransactionId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsSimulated;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSimulated);
            _tmpIsSimulated = _tmp != 0;
            _item = new TransferTransaction(_tmpId,_tmpSender,_tmpAmount,_tmpCurrency,_tmpTimestamp,_tmpDateString,_tmpTimeString,_tmpRawMessage,_tmpTransactionId,_tmpNotes,_tmpIsSimulated);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTransactionsByDateSync(final String date,
      final Continuation<? super List<TransferTransaction>> $completion) {
    final String _sql = "SELECT * FROM transfer_transactions WHERE dateString = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TransferTransaction>>() {
      @Override
      @NonNull
      public List<TransferTransaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final int _cursorIndexOfTimeString = CursorUtil.getColumnIndexOrThrow(_cursor, "timeString");
          final int _cursorIndexOfRawMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessage");
          final int _cursorIndexOfTransactionId = CursorUtil.getColumnIndexOrThrow(_cursor, "transactionId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsSimulated = CursorUtil.getColumnIndexOrThrow(_cursor, "isSimulated");
          final List<TransferTransaction> _result = new ArrayList<TransferTransaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransferTransaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final String _tmpTimeString;
            _tmpTimeString = _cursor.getString(_cursorIndexOfTimeString);
            final String _tmpRawMessage;
            _tmpRawMessage = _cursor.getString(_cursorIndexOfRawMessage);
            final String _tmpTransactionId;
            if (_cursor.isNull(_cursorIndexOfTransactionId)) {
              _tmpTransactionId = null;
            } else {
              _tmpTransactionId = _cursor.getString(_cursorIndexOfTransactionId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsSimulated;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSimulated);
            _tmpIsSimulated = _tmp != 0;
            _item = new TransferTransaction(_tmpId,_tmpSender,_tmpAmount,_tmpCurrency,_tmpTimestamp,_tmpDateString,_tmpTimeString,_tmpRawMessage,_tmpTransactionId,_tmpNotes,_tmpIsSimulated);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransferTransaction>> getTransactionsByDateRange(final String startDate,
      final String endDate) {
    final String _sql = "SELECT * FROM transfer_transactions WHERE dateString >= ? AND dateString <= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindString(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transfer_transactions"}, new Callable<List<TransferTransaction>>() {
      @Override
      @NonNull
      public List<TransferTransaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final int _cursorIndexOfTimeString = CursorUtil.getColumnIndexOrThrow(_cursor, "timeString");
          final int _cursorIndexOfRawMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessage");
          final int _cursorIndexOfTransactionId = CursorUtil.getColumnIndexOrThrow(_cursor, "transactionId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsSimulated = CursorUtil.getColumnIndexOrThrow(_cursor, "isSimulated");
          final List<TransferTransaction> _result = new ArrayList<TransferTransaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransferTransaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final String _tmpTimeString;
            _tmpTimeString = _cursor.getString(_cursorIndexOfTimeString);
            final String _tmpRawMessage;
            _tmpRawMessage = _cursor.getString(_cursorIndexOfRawMessage);
            final String _tmpTransactionId;
            if (_cursor.isNull(_cursorIndexOfTransactionId)) {
              _tmpTransactionId = null;
            } else {
              _tmpTransactionId = _cursor.getString(_cursorIndexOfTransactionId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsSimulated;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSimulated);
            _tmpIsSimulated = _tmp != 0;
            _item = new TransferTransaction(_tmpId,_tmpSender,_tmpAmount,_tmpCurrency,_tmpTimestamp,_tmpDateString,_tmpTimeString,_tmpRawMessage,_tmpTransactionId,_tmpNotes,_tmpIsSimulated);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTransactionsByDateRangeSync(final String startDate, final String endDate,
      final Continuation<? super List<TransferTransaction>> $completion) {
    final String _sql = "SELECT * FROM transfer_transactions WHERE dateString >= ? AND dateString <= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindString(_argIndex, endDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TransferTransaction>>() {
      @Override
      @NonNull
      public List<TransferTransaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final int _cursorIndexOfTimeString = CursorUtil.getColumnIndexOrThrow(_cursor, "timeString");
          final int _cursorIndexOfRawMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMessage");
          final int _cursorIndexOfTransactionId = CursorUtil.getColumnIndexOrThrow(_cursor, "transactionId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsSimulated = CursorUtil.getColumnIndexOrThrow(_cursor, "isSimulated");
          final List<TransferTransaction> _result = new ArrayList<TransferTransaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransferTransaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final String _tmpTimeString;
            _tmpTimeString = _cursor.getString(_cursorIndexOfTimeString);
            final String _tmpRawMessage;
            _tmpRawMessage = _cursor.getString(_cursorIndexOfRawMessage);
            final String _tmpTransactionId;
            if (_cursor.isNull(_cursorIndexOfTransactionId)) {
              _tmpTransactionId = null;
            } else {
              _tmpTransactionId = _cursor.getString(_cursorIndexOfTransactionId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsSimulated;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSimulated);
            _tmpIsSimulated = _tmp != 0;
            _item = new TransferTransaction(_tmpId,_tmpSender,_tmpAmount,_tmpCurrency,_tmpTimestamp,_tmpDateString,_tmpTimeString,_tmpRawMessage,_tmpTransactionId,_tmpNotes,_tmpIsSimulated);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Double> getDailyTotal(final String date) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transfer_transactions WHERE dateString = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transfer_transactions"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getDailyCount(final String date) {
    final String _sql = "SELECT COUNT(*) FROM transfer_transactions WHERE dateString = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transfer_transactions"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DailySummary>> getDailySummaries() {
    final String _sql = "\n"
            + "        SELECT dateString, \n"
            + "               COALESCE(SUM(amount), 0.0) AS totalAmount, \n"
            + "               COALESCE(currency, 'RM') AS currency, \n"
            + "               COUNT(*) AS transactionCount\n"
            + "        FROM transfer_transactions\n"
            + "        GROUP BY dateString\n"
            + "        ORDER BY dateString DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transfer_transactions"}, new Callable<List<DailySummary>>() {
      @Override
      @NonNull
      public List<DailySummary> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateString = 0;
          final int _cursorIndexOfTotalAmount = 1;
          final int _cursorIndexOfCurrency = 2;
          final int _cursorIndexOfTransactionCount = 3;
          final List<DailySummary> _result = new ArrayList<DailySummary>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailySummary _item;
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final int _tmpTransactionCount;
            _tmpTransactionCount = _cursor.getInt(_cursorIndexOfTransactionCount);
            _item = new DailySummary(_tmpDateString,_tmpTotalAmount,_tmpCurrency,_tmpTransactionCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object existsByMessageAndTimestamp(final String rawMessage, final long timestamp,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM transfer_transactions WHERE rawMessage = ? AND timestamp = ? LIMIT 1)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, rawMessage);
    _argIndex = 2;
    _statement.bindLong(_argIndex, timestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object existsByTransactionId(final String transactionId,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM transfer_transactions WHERE transactionId = ? AND transactionId IS NOT NULL AND transactionId != '' LIMIT 1)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, transactionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object isDuplicateTransaction(final String rawMessage, final long timestamp,
      final String transactionId, final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM transfer_transactions WHERE (rawMessage = ? AND timestamp = ?) OR (transactionId = ? AND transactionId IS NOT NULL AND transactionId != '') LIMIT 1)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, rawMessage);
    _argIndex = 2;
    _statement.bindLong(_argIndex, timestamp);
    _argIndex = 3;
    if (transactionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, transactionId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
