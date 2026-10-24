package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceStatus
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductEntity
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import com.example.data.model.QuotationStatus

class Converters {
    @TypeConverter
    fun fromInvoiceStatus(status: InvoiceStatus): String = status.name

    @TypeConverter
    fun toInvoiceStatus(value: String): InvoiceStatus = try {
        InvoiceStatus.valueOf(value)
    } catch (_: Exception) {
        InvoiceStatus.DRAFT
    }

    @TypeConverter
    fun fromQuotationStatus(status: QuotationStatus): String = status.name

    @TypeConverter
    fun toQuotationStatus(value: String): QuotationStatus = try {
        QuotationStatus.valueOf(value)
    } catch (_: Exception) {
        QuotationStatus.DRAFT
    }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try {
        PaymentMethod.valueOf(value)
    } catch (_: Exception) {
        PaymentMethod.BANK_TRANSFER
    }
}

@Database(
    entities = [
        BusinessProfileEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        QuotationEntity::class,
        QuotationItemEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun quotationDao(): QuotationDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "invoice_manager.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
