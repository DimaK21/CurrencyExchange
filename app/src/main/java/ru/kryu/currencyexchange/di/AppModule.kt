package ru.kryu.currencyexchange.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.kryu.currencyexchange.data.BalanceRepositoryImpl
import ru.kryu.currencyexchange.data.ExchangeRateRepositoryImpl
import ru.kryu.currencyexchange.data.network.ExchangeRateApi
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.cbr-xml-daily.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideExchangeRateApi(retrofit: Retrofit): ExchangeRateApi {
        return retrofit.create(ExchangeRateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("balances", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideExchangeRateRepository(api: ExchangeRateApi): ExchangeRateRepository =
        ExchangeRateRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideBalanceRepository(sharedPreferences: SharedPreferences): BalanceRepository =
        BalanceRepositoryImpl(sharedPreferences)
}