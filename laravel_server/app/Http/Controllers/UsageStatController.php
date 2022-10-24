<?php

namespace App\Http\Controllers;

use App\Models\UsageStat;
use Illuminate\Http\Request;

class UsageStatController extends Controller
{
    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        return response()->json(UsageStat::all(), 200);
    }

    /**
     * Show the form for creating a new resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \App\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function store(Request $request)
    {
        $request->flash();
        $body_json = json_decode($request->getContent());

        $usage = new UsageStat;

        $usage->plugin_id = $body_json->plugin_id;
        $usage->plugin_version_qualifier = $body_json->plugin_version_qualifier;
        $usage->plugin_version = $body_json->plugin_version;
        $usage->os_version = $body_json->os_version;
        $usage->os_arch = $body_json->os_arch;
        $usage->eclipse_version = $body_json->eclipse_version;
        $usage->eclipse_version_qualifier = $body_json->eclipse_version_qualifier;
        $usage->user_id = $body_json->user_id;
        $usage->java_version = $body_json->java_version;
        $usage->os_name = $body_json->os_name;
        $usage->info = $body_json->info;

        if ($usage->save()) {
            return response("Successfully added!",200)->header('Content-Type', 'text/plain');
        } else {
            abort(404);
        }
    }

    /**
     * Display the specified resource.
     *
     * @param  \App\Models\UsageStat  $usage_stat
     * @return \Illuminate\Http\Response
     */
    public function show($id)
    {
        return response()->json(UsageStat::findOrFail($id), 200);
    }

    /**
     * Show the form for editing the specified resource.
     *
     * @param  \App\Models\UsageStat  $usage_Stat
     * @return \Illuminate\Http\Response
     */
    public function edit(UsageStat $usage_Stat)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     *
     * @param  \App\Http\Requests\UpdateUsage_StatRequest  $request
     * @param  \App\Models\UsageStat  $usage_Stat
     * @return \Illuminate\Http\Response
     */
    public function update(UpdateUsage_StatRequest $request, UsageStat $usage_Stat)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     *
     * @param  \App\Models\UsageStat  $usage_Stat
     * @return \Illuminate\Http\Response
     */
    public function destroy($id)
    {
        $usage_stat = UsageStat::find($id);
        if ($usage_stat == null) {
            abort(204);
        }
        if (!$usage_stat->delete()) {
            abort(404);
        }
        return response("Successfully deleted!",200)->header('Content-Type', 'text/plain');
    }
}
