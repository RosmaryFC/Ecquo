package abassawo.c4q.nyc.ecquo.user.inputs;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class AppProvider extends ContentProvider {
    protected AppDatabaseHelper mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int NOTES = 100;
    private static final int NOTES_ID = 101;

    private static final int ARCHIVES = 200;
    private static final int ARCHIVES_ID = 201;

    private static final int TRASH = 300;
    private static final int TRASH_ID = 301;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = NotesContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "notes", NOTES);
        matcher.addURI(authority, "notes/*", NOTES_ID);
        authority = ArchivesContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "archives", ARCHIVES);
        matcher.addURI(authority, "archives/*", ARCHIVES_ID);
        authority = TrashContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "trash", TRASH);
        matcher.addURI(authority, "trash/*", TRASH_ID);
        return matcher;
    }

    private void deleteDatabase() {
        mOpenHelper.close();
        AppDatabaseHelper.deleteDatabase(getContext());
        mOpenHelper = new AppDatabaseHelper(getContext());
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AppDatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case NOTES:
                return NotesContract.Notes.CONTENT_TYPE;
            case NOTES_ID:
                return NotesContract.Notes.CONTENT_ITEM_TYPE;
            case ARCHIVES:
                return ArchivesContract.Archives.CONTENT_TYPE;
            case ARCHIVES_ID:
                return ArchivesContract.Archives.CONTENT_ITEM_TYPE;
            case TRASH:
                return TrashContract.Trash.CONTENT_TYPE;
            case TRASH_ID:
                return TrashContract.Trash.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(match) {
            case NOTES:
                queryBuilder.setTables(AppDatabaseHelper.Tables.NOTES);
                break;
            case NOTES_ID:
                queryBuilder.setTables(AppDatabaseHelper.Tables.NOTES);
                String note_id = NotesContract.Notes.getNoteId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + note_id);
                break;
            case ARCHIVES:
                queryBuilder.setTables(AppDatabaseHelper.Tables.ARCHIVES);
                break;
            case ARCHIVES_ID:
                queryBuilder.setTables(AppDatabaseHelper.Tables.ARCHIVES);
                String archive_id = ArchivesContract.Archives.getArchiveId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + archive_id);
                break;
            case TRASH:
                queryBuilder.setTables(AppDatabaseHelper.Tables.TRASH);
                break;
            case TRASH_ID:
                queryBuilder.setTables(AppDatabaseHelper.Tables.TRASH);
                String trash_id = TrashContract.Trash.getTrashId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + trash_id);
                break;

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                long noteRecordId = db.insertOrThrow(AppDatabaseHelper.Tables.NOTES, null, values);
                return NotesContract.Notes.buildNoteUri(String.valueOf(noteRecordId));

            case ARCHIVES:
                long archiveRecordId = db.insertOrThrow(AppDatabaseHelper.Tables.ARCHIVES, null, values);
                return ArchivesContract.Archives.buildArchiveUri(String.valueOf(archiveRecordId));

            case TRASH:
                long trashRecordId = db.insertOrThrow(AppDatabaseHelper.Tables.TRASH, null, values);
                return TrashContract.Trash.buildTrashUri(String.valueOf(trashRecordId));

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        String selectionCriteria = selection;
        switch (match) {
            case NOTES:
                return db.update(AppDatabaseHelper.Tables.NOTES, values, selection, selectionArgs);

            case NOTES_ID:
                String noteId = NotesContract.Notes.getNoteId(uri);
                selectionCriteria = BaseColumns._ID + "=" + noteId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.update(AppDatabaseHelper.Tables.NOTES, values, selectionCriteria, selectionArgs);

            case ARCHIVES:
                return db.update(AppDatabaseHelper.Tables.ARCHIVES, values, selection, selectionArgs);

            case ARCHIVES_ID:
                String archiveId = ArchivesContract.Archives.getArchiveId(uri);
                selectionCriteria = BaseColumns._ID + "=" + archiveId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.update(AppDatabaseHelper.Tables.ARCHIVES, values, selectionCriteria, selectionArgs);

            case TRASH:
                return db.update(AppDatabaseHelper.Tables.TRASH, values, selection, selectionArgs);

            case TRASH_ID:
                String trashId = TrashContract.Trash.getTrashId(uri);
                selectionCriteria = BaseColumns._ID + "=" + trashId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.update(AppDatabaseHelper.Tables.TRASH, values, selectionCriteria, selectionArgs);

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        if(uri.equals(NotesContract.BASE_CONTENT_URI)) {
            deleteDatabase();
            return 0;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case NOTES_ID:
                String noteId = NotesContract.Notes.getNoteId(uri);
                String notesSelectionCriteria = BaseColumns._ID + "=" + noteId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(AppDatabaseHelper.Tables.NOTES, notesSelectionCriteria, selectionArgs);

            case ARCHIVES_ID:
                String archiveId = ArchivesContract.Archives.getArchiveId(uri);
                String archiveSelectionCriteria = BaseColumns._ID + "=" + archiveId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(AppDatabaseHelper.Tables.ARCHIVES, archiveSelectionCriteria, selectionArgs);

            case TRASH_ID:
                String trashId = TrashContract.Trash.getTrashId(uri);
                String trashSelectionCriteria = BaseColumns._ID + "=" + trashId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(AppDatabaseHelper.Tables.TRASH, trashSelectionCriteria, selectionArgs);

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }
}
