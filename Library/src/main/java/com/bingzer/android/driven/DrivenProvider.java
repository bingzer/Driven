package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.DrivenApi;

public interface DrivenProvider extends DrivenApi.Auth,
                                    DrivenApi.Exists,
                                    DrivenApi.Get, DrivenApi.Get.ById,
                                    DrivenApi.Post, DrivenApi.Put,
                                    DrivenApi.Delete, DrivenApi.Query,
                                    DrivenApi.List, DrivenApi.Details,
                                    DrivenApi.Download, DrivenApi.Share {

    DrivenUser getDrivenUser();

}
