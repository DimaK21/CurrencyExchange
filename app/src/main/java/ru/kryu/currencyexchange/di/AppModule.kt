package ru.kryu.currencyexchange.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.kryu.currencyexchange.data.BalanceRepositoryImpl
import ru.kryu.currencyexchange.data.ExchangeRateRepositoryImpl
import ru.kryu.currencyexchange.data.NetworkClient
import ru.kryu.currencyexchange.data.network.ExchangeRateApi
import ru.kryu.currencyexchange.data.network.RetrofitNetworkClient
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.CurrencyConverter
import ru.kryu.currencyexchange.domain.CurrencyConverterImpl
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Singleton

@Module
object AppModule {

    private const val BASE_URL = "https://www.cbr-xml-daily.ru/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideExchangeRateApi(retrofit: Retrofit): ExchangeRateApi {
        return retrofit.create(ExchangeRateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideNetworkClient(api: ExchangeRateApi): NetworkClient =
        RetrofitNetworkClient(api)

    @Provides
    @Singleton
    fun provideExchangeRateRepository(networkClient: NetworkClient): ExchangeRateRepository =
        ExchangeRateRepositoryImpl(networkClient)

    @Provides
    @Singleton
    fun provideBalanceRepository(dataStore: DataStore<Preferences>): BalanceRepository =
        BalanceRepositoryImpl(dataStore)

    @Provides
    @Singleton
    fun provideCurrencyConverter(exchangeRateRepository: ExchangeRateRepository): CurrencyConverter =
        CurrencyConverterImpl(exchangeRateRepository)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "balances")