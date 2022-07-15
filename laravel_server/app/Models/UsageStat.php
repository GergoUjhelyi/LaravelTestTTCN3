<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class UsageStat extends Model
{
    use HasFactory;

    protected $table = 'usage_stats';

    protected $fillable = [
        'plugin_id',
        'plugin_version_qualifier',
        'plugin_version',
        'os_version',
        'os_arch',
        'eclipse_version',
        'eclipse_version_qualifier',
        'user_id',
        'java_version',
        'os_name',
        'info'
    ];
}
